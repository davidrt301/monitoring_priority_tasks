package com.davidrt301.priority_tasks.mappers;

import com.davidrt301.priority_tasks.model.dtos.CategoryResponse;
import com.davidrt301.priority_tasks.model.dtos.CategoryRequest;
import com.davidrt301.priority_tasks.model.entities.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    Category toEntity(CategoryRequest request);

    CategoryResponse toResponse(Category category);
}