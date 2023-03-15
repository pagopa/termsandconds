package it.gov.pagopa.swclient.mil.termsandconds.dao;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;

/**
 *  MongoDB repository to access T&C current version data, reactive flavor
 */
@ApplicationScoped
public class TCVersionRepository implements ReactivePanacheMongoRepository<TCEntityVersion> {

}
