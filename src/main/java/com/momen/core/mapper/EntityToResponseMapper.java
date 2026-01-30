package com.momen.core.mapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Entity → Response DTO 변환 공통 인터페이스
 * MapStruct 또는 수동 매퍼가 이 규약을 따르면 서비스에서 일관되게 사용 가능
 */
public interface EntityToResponseMapper<E, R> {

    R toResponse(E entity);

    default List<R> toResponse(List<E> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
