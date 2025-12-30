{{- define "svc-docs.name" -}}
svc-docs
{{- end }}

{{- define "svc-docs.fullname" -}}
{{ include "svc-docs.name" . }}
{{- end }}
