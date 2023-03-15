package it.gov.pagopa.swclient.mil.termsandconds.dao;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;

/**
 *  MongoDB repository to access T&C version associated to the Tax Code Token data, reactive flavor
 */
@ApplicationScoped
public class TCRepository implements ReactivePanacheMongoRepository<TcTaxCodeTokenEntity> {
}