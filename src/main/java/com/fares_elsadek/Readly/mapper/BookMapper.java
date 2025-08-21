package com.fares_elsadek.Readly.mapper;

import com.fares_elsadek.Readly.dtos.BookRequestDto;
import com.fares_elsadek.Readly.dtos.BookResponseDto;
import com.fares_elsadek.Readly.entity.Book;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface BookMapper {
    @InheritInverseConfiguration
    Book toEntity(BookRequestDto bookRequestDto);

    @Mappings({
            @Mapping(source = "owner.firstname",target = "owner.firstname"),
            @Mapping(source = "owner.lastname",target = "owner.lastname"),
            @Mapping(source = "owner.id",target = "owner.id"),
            @Mapping(source = "owner.email",target = "owner.email"),
    })
    BookResponseDto toBookResponse(Book book);
}
