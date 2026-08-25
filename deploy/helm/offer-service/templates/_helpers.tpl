{{- define "offer-service.name" -}}
offer-service
{{- end -}}

{{- define "offer-service.fullname" -}}
{{ .Release.Name }}
{{- end -}}

{{- define "offer-service.labels" -}}
app.kubernetes.io/name: {{ include "offer-service.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}
