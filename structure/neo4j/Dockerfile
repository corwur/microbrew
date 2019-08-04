FROM neo4j
ADD docker-entrypoint.sh /docker-entrypoint.sh
ADD yeast.db.dump /yeast.db.dump
ADD neo4j.conf /var/lib/neo4j/conf/neo4j.conf
#use local.data because the neo4j Dockerfile defines /data as a volume (this is preferrable but not for testing a basic setup)
RUN mkdir -p /local.data/databases
RUN neo4j-admin load --from=/yeast.db.dump --database=yeast.db
RUN chown -R neo4j /local.data
EXPOSE 7474 7473 7687
ENTRYPOINT ["/sbin/tini", "-g", "--", "/docker-entrypoint.sh"]
CMD ["neo4j", "console" ]


