package com.vider.quantum.engine.service;

import com.vider.quantum.engine.entities.vider.GstrCredentials;
import com.vider.quantum.engine.repository.vider.GstrCredentialsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GstrCredentialsService {

    @Autowired
    private GstrCredentialsRepository gstrCredentialsRepository;

    public List<GstrCredentials> getAllGstrCredentials() {
        return gstrCredentialsRepository.findAll();
    }

    public Optional<GstrCredentials> getGstrCredentialsById(Integer id) {
        return gstrCredentialsRepository.findById(id);
    }

    public List<GstrCredentials> findAllByOrganizationId(Integer organizationId) {
        return gstrCredentialsRepository.findAllByOrganizationIdAndStatus(organizationId, GstrCredentials.Status.ENABLE);
    }

    public GstrCredentials createGstrCredentials(GstrCredentials gstrCredentials) {
        return gstrCredentialsRepository.save(gstrCredentials);
    }

    public GstrCredentials updateGstrCredentials(Integer id, GstrCredentials gstrCredentials) {
        Optional<GstrCredentials> existingGstrCredentials = gstrCredentialsRepository.findById(id);
        if (existingGstrCredentials.isPresent()) {
            GstrCredentials updatedGstrCredentials = existingGstrCredentials.get();
            updatedGstrCredentials.setUserName(gstrCredentials.getUserName());
            updatedGstrCredentials.setPassword(gstrCredentials.getPassword());
            updatedGstrCredentials.setClientId(gstrCredentials.getClientId());
            updatedGstrCredentials.setUserId(gstrCredentials.getUserId());
            updatedGstrCredentials.setOrganizationId(gstrCredentials.getOrganizationId());
            updatedGstrCredentials.setAuthToken(gstrCredentials.getAuthToken());
            return gstrCredentialsRepository.save(updatedGstrCredentials);
        }
        return null;
    }

    public void deleteGstrCredentials(Integer id) {
        gstrCredentialsRepository.deleteById(id);
    }
}
