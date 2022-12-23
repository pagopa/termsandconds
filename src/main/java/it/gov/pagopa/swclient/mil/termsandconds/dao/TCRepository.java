package it.gov.pagopa.swclient.mil.termsandconds.dao;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;

@ApplicationScoped
public class TCRepository implements ReactivePanacheMongoRepository<TCEntity> {

}
