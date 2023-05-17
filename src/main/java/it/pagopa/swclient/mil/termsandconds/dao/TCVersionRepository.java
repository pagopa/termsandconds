package it.pagopa.swclient.mil.termsandconds.dao;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 *  MongoDB repository to access T&C current version data, reactive flavor
 */
@ApplicationScoped
public class TCVersionRepository implements ReactivePanacheMongoRepository<TCEntityVersion> {

}
