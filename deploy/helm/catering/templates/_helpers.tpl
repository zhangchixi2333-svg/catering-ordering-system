{{- define "catering.labels" -}}
app.kubernetes.io/name: {{ .name }}
app.kubernetes.io/instance: {{ .root.Release.Name }}
app.kubernetes.io/managed-by: {{ .root.Release.Service }}
{{- end }}

{{- define "catering.selectorLabels" -}}
app: {{ .name }}
{{- end }}
