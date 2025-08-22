package com.system.batch.kill_batch_system.part02.chunk;

import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

/**
 * 데이터를 읽어온다
 */
public interface ItemReader<T> {

    /**
     * read() 메서드는 아이템을 하나씩 반환
     * 파일의 한 줄 또는 DB의 한 행(row)과 같이 데이터 하나
     * 없으면 null 반환 -> step 종료
     * ItemReader가 null을 반환하는 것이 청크 지향 처리 Step의 종료 시점(*) -> Spring Batch가 Step의 완료를 판단하는 핵심 조건
     */
    T read() throws Exception,
            UnexpectedInputException,
            ParseException,
            NonTransientResourceException;

}