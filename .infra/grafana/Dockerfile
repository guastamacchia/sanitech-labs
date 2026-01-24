FROM grafana/grafana:11.2.0

# -----------------------------------------------------
# Provisioning automatico
# -----------------------------------------------------
# Copia i file di provisioning (datasource, dashboard, ecc.)
# all'interno dell'immagine.
#
# Questo consente a Grafana di:
# - avviarsi già configurato
# - collegarsi automaticamente a Prometheus o altri datasource
# - evitare configurazioni manuali post-avvio
#
# I file copiati seguono la struttura standard di Grafana:
# /etc/grafana/provisioning/
COPY provisioning /etc/grafana/provisioning
