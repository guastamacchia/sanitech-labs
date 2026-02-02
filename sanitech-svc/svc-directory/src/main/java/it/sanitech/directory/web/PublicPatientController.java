package it.sanitech.directory.web;

import it.sanitech.directory.integrations.captcha.CaptchaService;
import it.sanitech.directory.services.PatientService;
import it.sanitech.directory.services.dto.PatientDto;
import it.sanitech.directory.services.dto.create.PatientCreateDto;
import it.sanitech.directory.services.dto.create.PublicPatientRegistrationDto;
import it.sanitech.directory.utilities.AppConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * Endpoint pubblico per la registrazione pazienti.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(AppConstants.ApiPath.PUBLIC_PATIENTS)
public class PublicPatientController {

    private final PatientService patientService;
    private final CaptchaService captchaService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public PatientDto register(@Valid @RequestBody PublicPatientRegistrationDto request) {
        // Verifica CAPTCHA prima di procedere con la registrazione
        captchaService.verify(request.captchaToken());

        PatientCreateDto dto = new PatientCreateDto(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phone(),
                request.fiscalCode(),
                request.birthDate(),
                request.address(),
                Set.of()
        );
        return patientService.createPublic(dto);
    }
}
