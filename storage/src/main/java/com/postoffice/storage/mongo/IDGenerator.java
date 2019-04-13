package com.postoffice.storage.mongo;

import com.postoffice.storage.mongo.dao.IDRepository;
import com.postoffice.storage.mongo.entity.IDSequenceDBEntity;
import org.assertj.core.util.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;


@Repository
public class IDGenerator {

    private Logger logger = Loggers.getLogger(IDGenerator.class);

    @Autowired
    private ReactiveMongoTemplate template;

    @Autowired
    private IDRepository reactiveCrudRepository;

    public Mono<IDSequenceDBEntity> getNextSequenceMono(String seqName){

        Query query = new Query(Criteria.where("sequenceName").is(seqName));
        Update update = new Update().inc("sequenceValue", 1)
                /*.setOnInsert("sequenceName",seqName)
                .setOnInsert("sequenceValue",0)
                .setOnInsert("generateDate",new Date())*/;
        return template.findAndModify(query,update, IDSequenceDBEntity.class)
                .doOnError(throwable -> logger.error("Error when generate sequence for {} ",seqName));
    }

    public Flux<IDSequenceDBEntity> getNextSequenceFlux(String seqName){
        return getNextSequenceMono(seqName).repeat();
    }

    /**
     * 初始化ID生成器信息，可用mongodb shell直接操作
     * @param seqName
     * @return
     */
    @VisibleForTesting
    public Mono<IDSequenceDBEntity> generateSEQ(String seqName){
        IDSequenceDBEntity idSequence = new IDSequenceDBEntity();
        idSequence.setSequenceName(seqName);
        idSequence.setSequenceValue(0L);
        /*return template.insert(idSequence).onErrorMap(c->new RuntimeException("can not generate a sequence",c));*/
        return reactiveCrudRepository.save(idSequence);
    }
}
