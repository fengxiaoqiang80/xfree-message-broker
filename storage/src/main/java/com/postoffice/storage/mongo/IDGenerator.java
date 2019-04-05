package com.postoffice.storage.mongo;

import org.assertj.core.util.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Repository
public class IDGenerator {

    @Autowired
    private ReactiveMongoTemplate template;

    @Autowired
    private IDRepository reactiveCrudRepository;

    public Mono<IDSequence> getNextSequenceMono(String seqName){
        Query query = new Query(Criteria.where("sequenceName").is(seqName));
        Update update = new Update().inc("sequenceValue", 1);
        return template.findAndModify(query,update, IDSequence.class);
    }

    public Flux<IDSequence> getNextSequenceFlux(String seqName){
        return getNextSequenceMono(seqName).repeat();
    }

    @VisibleForTesting
    public Mono<IDSequence> generateSEQ(String seqName){
        IDSequence idSequence = new IDSequence();
        idSequence.setSequenceName(seqName);
        idSequence.setSequenceValue(0L);
        /*return template.insert(idSequence).onErrorMap(c->new RuntimeException("can not generate a sequence",c));*/

        return reactiveCrudRepository.save(idSequence);
    }










}
