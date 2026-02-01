FROM prom/prometheus:v2.55.1

# -----------------------------------------------------
# Configurazione statica
# -----------------------------------------------------
# Copia il file di configurazione di Prometheus all'interno
# dell'immagine Docker.
#
# In questo modo:
# - non è necessario montare volumi o bind mount a runtime
# - la configurazione diventa parte integrante dell'immagine
# - l'ambiente risulta più semplice e prevedibile
COPY prometheus.yml /etc/prometheus/prometheus.yml

# -----------------------------------------------------
# Avvio di Prometheus
# -----------------------------------------------------
# Utilizza l'entrypoint di default dell'immagine ufficiale
# e specifica esplicitamente il file di configurazione da usare.
CMD [ "--config.file=/etc/prometheus/prometheus.yml" ]
