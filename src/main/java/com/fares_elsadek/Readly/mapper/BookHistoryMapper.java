package com.fares_elsadek.Readly.mapper;

import com.fares_elsadek.Readly.dtos.BookHistoryDto;
import com.fares_elsadek.Readly.entity.BookTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface BookHistoryMapper {

    @Mappings({
            @Mapping(source = "user.firstname",target = "user.firstname"),
            @Mapping(source = "user.lastname",target = "user.lastname"),
            @Mapping(source = "user.id",target = "user.id"),
            @Mapping(source = "user.email",target = "user.email"),
    })
    BookHistoryDto toDto(BookTransaction transaction);
}
