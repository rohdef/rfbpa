{{- define "rfbpa.name" -}}
{{- .Chart.Name | trunc 63 | trimSuffix "-" }}
{{- end }}


{{- define "rfbpa.fullname" -}}
{{- $name := .Chart.Name }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}

{{- define "rfbpa.restApi.name" -}}
{{ print "rest-api-" (include "rfbpa.fullname" .) | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "rfbpa.webApp.name" -}}
{{ print "web-app-" (include "rfbpa.fullname" .) | trunc 63 | trimSuffix "-" }}
{{- end }}


{{- define "rfbpa.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}


{{- define "rfbpa.labels" -}}
helm.sh/chart: {{ include "rfbpa.chart" . }}
{{ include "rfbpa.selectorLabels" . }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}


{{- define "rfbpa.selectorLabels" -}}
app.kubernetes.io/name: {{ include "rfbpa.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
