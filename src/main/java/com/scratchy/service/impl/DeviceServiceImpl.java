package com.scratchy.service.impl;

import com.scratchy.config.EnvironmentConfig;
import com.scratchy.model.Device;
import com.scratchy.model.DeviceFileDto;
import com.scratchy.repository.DeviceFileRepository;
import com.scratchy.service.DeviceService;
import lombok.extern.slf4j.Slf4j;
import nonapi.io.github.classgraph.json.JSONSerializer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DeviceServiceImpl implements DeviceService {

    private static final String TOPIC_NAME = "device-topic";

    private final WebClient webClient;

    private DeviceFileRepository repository;
    private KafkaTemplate<String, String> kafkaTemplate;

    private JavaMailSender emailSender;

    public DeviceServiceImpl(EnvironmentConfig config) {
        this.webClient = WebClient.builder()
                .baseUrl(config.getUrl() + config.getPort())
                .build();
    }

    @Autowired
    public void setRepository(DeviceFileRepository repository) {
        this.repository = repository;
    }

    @Autowired
    public void setKafkaTemplate(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Autowired
    public void setEmailSender(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Override
    public ResponseEntity<Device> getDeviceBySerialNumber(String serialNumber) {
        Device device;
        try {
            device = webClient.get()
                    .uri("/api/devices/" + serialNumber)
                    .retrieve()
                    .bodyToMono(Device.class)
                    .block();
        } catch (WebClientResponseException exception) {
            return ResponseEntity.status(exception.getStatusCode()).body(null);
        }

        return ResponseEntity.ok(device);
    }

    @Override
    public ResponseEntity<Iterable<Device>> getDeviceListByModelInJson(String model) {
        return ResponseEntity.ok().body(getDeviceListByModel(model));
    }

    @Override
    public String getDeviceListByModelInCsv(String model) {
        List<Device> deviceList = getDeviceListByModel(model);
        StringBuilder builder = new StringBuilder();

        try (CSVPrinter printer = new CSVPrinter(builder,
                CSVFormat.DEFAULT.withHeader("id", "model", "description"))) {
            deviceList.forEach(value -> {
                try {
                    printer.printRecord(value.getId(), value.getModel(),
                            value.getDescription());
                } catch (IOException exception) {
                    throw new RuntimeException("During printing record something went wrong..",
                            exception);
                }
            });
        } catch (IOException exception) {
            throw new RuntimeException("During creating csv something went wrong..",
                    exception);
        }

        return builder.toString();
    }

    private List<Device> getDeviceListByModel(String model) {
        List<Device> deviceList = Objects.requireNonNull(webClient
                .get()
                .uri("/api/devices")
                .retrieve()
                .toEntityList(Device.class)
                .block()).getBody();

        return deviceList
                .stream()
                .filter(value -> value.getModel().equals(model))
                .collect(Collectors.toList());
    }

    @Override
    public Iterable<Device> createDevicesFromCsvFile(MultipartFile file, Principal principal) {
        List<Device> devices;
        try {
            devices = parseCsvFile(file);
        } catch (IOException exception) {
            throw new RuntimeException("During parsing an csv file an exception occurred",
                    exception);
        }
        log.info("Sending device list to " + TOPIC_NAME);
        sendDeviceList(devices);

        log.info("Writing new record to the device controller database");
        DeviceFileDto fileDto = new DeviceFileDto(file.getOriginalFilename(),
                devices.size(), principal.getName());
        repository.save(fileDto);
        return devices;
    }

    private List<Device> parseCsvFile(MultipartFile file) throws IOException {
        List<Device> deviceList = new ArrayList<>();
        CSVParser csvParser = CSVFormat.DEFAULT.withFirstRecordAsHeader()
                .parse(new InputStreamReader(new ByteArrayInputStream(file.getBytes())));
        for (CSVRecord record : csvParser) {
            String id = record.get("id");
            String model = record.get("model");
            String description = record.get("description");
            deviceList.add(new Device(id, model, description));
        }

        return deviceList;
    }

    private void sendDeviceList(List<Device> deviceList) {
        ListenableFuture<SendResult<String, String>> future =
                kafkaTemplate.send(TOPIC_NAME,
                        JSONSerializer.serializeObject(deviceList.toArray()));

        future.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onFailure(Throwable exception) {
                throw new RuntimeException("During sending message something went wrong",
                        exception);
            }

            @Override
            public void onSuccess(SendResult<String, String> stringStringSendResult) {
                log.info("The device list was successfully sent");
            }
        });
    }

    @Override
    public void sendEmail() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("some_mail@test.mail");
        message.setSubject("CSV was uploaded");
        message.setText("Your csv file was successfully uploaded");
        emailSender.send(message);
    }

    @Override
    public Iterable<DeviceFileDto> getListOfUploadedFiles() {
        return repository.findAll();
    }
}
