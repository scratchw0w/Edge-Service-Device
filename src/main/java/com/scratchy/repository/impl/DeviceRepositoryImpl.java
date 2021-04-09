package com.scratchy.repository.impl;

import com.scratchy.model.Device;
import com.scratchy.repository.DeviceRepository;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Optional;

@Repository
@Transactional
public class DeviceRepositoryImpl implements DeviceRepository {

    private EntityManager entityManager;

    @Autowired
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Iterable<Device>> getDeviceList() {
        Query<Device> deviceQuery = getSession()
                .createQuery("from Device", Device.class);
        return Optional.ofNullable(deviceQuery.getResultList());
    }

    @Override
    public Optional<Device> getDeviceById(String id) {
        return Optional.ofNullable(getSession().get(Device.class, id));
    }

    @Override
    public void saveDevice(Device device) {
        getSession().save(device);
    }

    @Override
    public Optional<Device> updateDevice(String id, Device device) {
        Session currentSession = getSession();
        Optional<Device> optionalDevice = Optional
                .ofNullable(currentSession.get(Device.class, id));
        if (optionalDevice.isPresent()) {
            Device deviceToUpdate = optionalDevice.get();
            deviceToUpdate.setModel(device.getModel());
            deviceToUpdate.setDescription(device.getDescription());
            currentSession.update(deviceToUpdate);
        }

        return optionalDevice;
    }

    @Override
    public Optional<Device> deleteDeviceById(String id) {
        Optional<Device> optionalDevice = Optional
                .ofNullable(getSession().get(Device.class, id));
        optionalDevice.ifPresent(device -> getSession().delete(device));

        return optionalDevice;
    }

    private Session getSession() {
        return entityManager.unwrap(Session.class);
    }
}
