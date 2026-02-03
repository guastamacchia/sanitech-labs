<#macro registrationLayout bodyClass="" displayInfo=false displayMessage=true displayRequiredFields=false>
<!DOCTYPE html>
<html class="${properties.kcHtmlClass!}">
<head>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="robots" content="noindex, nofollow">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <#if properties.meta?has_content>
        <#list properties.meta?split(' ') as meta>
            <meta name="${meta?split('==')[0]}" content="${meta?split('==')[1]}"/>
        </#list>
    </#if>
    <title>${msg("loginTitle",(realm.displayName!''))}</title>
    <link rel="icon" href="${url.resourcesPath}/img/favicon.ico" />
    <#if properties.stylesCommon?has_content>
        <#list properties.stylesCommon?split(' ') as style>
            <link href="${url.resourcesCommonPath}/${style}" rel="stylesheet" />
        </#list>
    </#if>
    <#if properties.styles?has_content>
        <#list properties.styles?split(' ') as style>
            <link href="${url.resourcesPath}/${style}" rel="stylesheet" />
        </#list>
    </#if>
</head>
<body class="sanitech-login-page">
    <div class="sanitech-container">
        <!-- Left Panel - Login Form -->
        <div class="sanitech-form-panel">
            <div class="sanitech-form-container">
                <!-- Back to home link -->
                <a href="http://localhost:4200" class="sanitech-back-link">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" viewBox="0 0 16 16">
                        <path fill-rule="evenodd" d="M15 8a.5.5 0 0 0-.5-.5H2.707l3.147-3.146a.5.5 0 1 0-.708-.708l-4 4a.5.5 0 0 0 0 .708l4 4a.5.5 0 0 0 .708-.708L2.707 8.5H14.5A.5.5 0 0 0 15 8"/>
                    </svg>
                    Torna alla Home
                </a>

                <!-- Logo -->
                <div class="sanitech-form-logo">
                    <img src="${url.resourcesPath}/img/sanitech-logo.svg" alt="SaniTech Labs">
                    <span>SaniTech Labs</span>
                </div>

                <!-- Header -->
                <div class="sanitech-form-header">
                    <#nested "header">
                </div>

                <!-- Alerts -->
                <#if displayMessage && message?has_content && (message.type != 'warning' || !isAppInitiatedAction??)>
                    <div class="sanitech-alert sanitech-alert-${message.type}">
                        <#if message.type = 'success'>
                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" viewBox="0 0 16 16">
                                <path d="M16 8A8 8 0 1 1 0 8a8 8 0 0 1 16 0m-3.97-3.03a.75.75 0 0 0-1.08.022L7.477 9.417 5.384 7.323a.75.75 0 0 0-1.06 1.06L6.97 11.03a.75.75 0 0 0 1.079-.02l3.992-4.99a.75.75 0 0 0-.01-1.05z"/>
                            </svg>
                        <#elseif message.type = 'warning'>
                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" viewBox="0 0 16 16">
                                <path d="M8.982 1.566a1.13 1.13 0 0 0-1.96 0L.165 13.233c-.457.778.091 1.767.98 1.767h13.713c.889 0 1.438-.99.98-1.767zM8 5c.535 0 .954.462.9.995l-.35 3.507a.552.552 0 0 1-1.1 0L7.1 5.995A.905.905 0 0 1 8 5m.002 6a1 1 0 1 1 0 2 1 1 0 0 1 0-2"/>
                            </svg>
                        <#elseif message.type = 'error'>
                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" viewBox="0 0 16 16">
                                <path d="M16 8A8 8 0 1 1 0 8a8 8 0 0 1 16 0M5.354 4.646a.5.5 0 1 0-.708.708L7.293 8l-2.647 2.646a.5.5 0 0 0 .708.708L8 8.707l2.646 2.647a.5.5 0 0 0 .708-.708L8.707 8l2.647-2.646a.5.5 0 0 0-.708-.708L8 7.293z"/>
                            </svg>
                        <#else>
                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" viewBox="0 0 16 16">
                                <path d="M8 16A8 8 0 1 0 8 0a8 8 0 0 0 0 16m.93-9.412-1 4.705c-.07.34.029.533.304.533.194 0 .487-.07.686-.246l-.088.416c-.287.346-.92.598-1.465.598-.703 0-1.002-.422-.808-1.319l.738-3.468c.064-.293.006-.399-.287-.47l-.451-.081.082-.381 2.29-.287zM8 5.5a1 1 0 1 1 0-2 1 1 0 0 1 0 2"/>
                            </svg>
                        </#if>
                        <span>${kcSanitize(message.summary)?no_esc}</span>
                    </div>
                </#if>

                <!-- Form -->
                <#nested "form">

                <!-- Info/Registration -->
                <#if displayInfo>
                    <#nested "info">
                </#if>

                <!-- Footer -->
                <div class="sanitech-form-footer">
                    <p>&copy; 2026 SaniTech Labs. Tutti i diritti riservati.</p>
                </div>
            </div>
        </div>

        <!-- Right Panel - Hero Image with Description -->
        <div class="sanitech-hero-panel">
            <div class="sanitech-hero-overlay"></div>
            <div class="sanitech-hero-content">
                <h1 class="sanitech-hero-title">Il Network Sanitario del Futuro</h1>
                <p class="sanitech-hero-description">
                    SaniTech Labs connette un network di strutture sanitarie d'eccellenza,
                    offrendo ai pazienti un accesso semplice e sicuro a servizi medici di qualità
                    su tutto il territorio nazionale.
                </p>
                <div class="sanitech-hero-stats">
                    <div class="sanitech-stat">
                        <span class="sanitech-stat-number">150+</span>
                        <span class="sanitech-stat-label">Strutture Sanitarie</span>
                    </div>
                    <div class="sanitech-stat">
                        <span class="sanitech-stat-number">2.500+</span>
                        <span class="sanitech-stat-label">Medici Specialisti</span>
                    </div>
                    <div class="sanitech-stat">
                        <span class="sanitech-stat-number">1M+</span>
                        <span class="sanitech-stat-label">Pazienti Assistiti</span>
                    </div>
                </div>
                <p class="sanitech-hero-tagline">
                    Una piattaforma unica per prenotazioni, televisite, cartelle cliniche digitali
                    e prescrizioni elettroniche. La tua salute, sempre a portata di mano.
                </p>
            </div>
        </div>
    </div>
</body>
</html>
</#macro>
