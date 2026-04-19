FROM consul:1.15.4

RUN mkdir /tmp/bootstrap

COPY bootstrap/* /tmp/bootstrap/
RUN chmod 755 /tmp/bootstrap/*
RUN dos2unix /tmp/bootstrap/*

CMD /tmp/bootstrap/start.sh