package com.syndicati.models.services;

import com.syndicati.models.entities.Onboarding;
import com.syndicati.models.repositories.OnboardingRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Onboarding service aligned with web admin onboarding edit flow.
 */
public class OnboardingService {

    private final OnboardingRepository onboardingRepository;

    public OnboardingService() {
        this.onboardingRepository = new OnboardingRepository();
    }

    public List<Onboarding> listOnboardings() {
        return onboardingRepository.findAllByIdDesc();
    }

    public Optional<Onboarding> findById(int idOnboarding) {
        return onboardingRepository.findById(idOnboarding);
    }

    public Optional<Onboarding> findOneByUserId(int userId) {
        return onboardingRepository.findOneByUserId(userId);
    }

    public boolean updateOnboarding(Onboarding onboarding) {
        if (onboarding == null || onboarding.getIdOnboarding() == null || onboarding.getIdOnboarding() <= 0) {
            return false;
        }

        if (onboarding.getUserId() == null || onboarding.getUserId() <= 0) {
            return false;
        }

        onboarding.setUpdatedAt(LocalDateTime.now());
        return onboardingRepository.update(onboarding);
    }
}
