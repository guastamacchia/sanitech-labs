{{- define "svc-prescribing.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "svc-prescribing.fullname" -}}
{{- if .Values.fullnameOverride -}}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s" (include "svc-prescribing.name" .) | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}
