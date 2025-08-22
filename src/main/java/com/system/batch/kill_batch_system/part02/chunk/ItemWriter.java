package com.system.batch.kill_batch_system.part02.chunk;

import org.springframework.batch.item.Chunk;

/**
 * ItemProcessor가 만든 결과물을 받아, 원하는 방식으로 최종 저장/출력한다.
 * 데이터를 DB에 INSERT, 파일에 WRITE, 메시지 큐에 PUSH
 */
public interface ItemWriter<T> {

    /**
     * 1. 한 덩어리씩 쓴다
     * ItemWriter는 데이터를 한 건씩 쓰지 않는다. Chunk 단위로 묶어서 한 번에 데이터를 쓴다.
     * write() 메서드의 파라미터 타입이 Chunk 인 것 주목.
     * ItemReader와 ItemProcessor가 아이템을 하나씩 반환하고 입력받는 것과 달리, ItemWriter는 데이터 덩어리(Chunk)를 한 번에 입력받아 한 번에 쓴다.
     *
     * 2. 다양한 구현체 제공
     * Spring Batch는 파일, 데이터베이스, 외부 시스템 전송 등에 사용할 수 있는 다양한 구현체를 제공한다.
     * 예를 들어, FlatFileItemWriter는 파일에 데이터를 기록하고, JdbcBatchItemWriter는 데이터베이스에 데이터를 저장한다.
     */
    void write(Chunk<? extends T> chunk) throws Exception;
}
