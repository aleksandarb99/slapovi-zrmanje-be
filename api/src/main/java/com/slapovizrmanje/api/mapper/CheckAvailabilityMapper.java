package com.slapovizrmanje.api.mapper;

import com.slapovizrmanje.api.dto.CheckAvailabilityForCampDto;
import com.slapovizrmanje.api.dto.GuestsForCampDto;
import com.slapovizrmanje.api.dto.LodgingForCampDto;
import com.slapovizrmanje.api.model.CheckAvailabilityForCamp;
import com.slapovizrmanje.api.model.GuestsForCamp;
import com.slapovizrmanje.api.model.LodgingForCamp;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.FIELD)
public interface CheckAvailabilityMapper {

  CheckAvailabilityForCamp toEntity(CheckAvailabilityForCampDto dto);

  GuestsForCamp toEntity(GuestsForCampDto dto);

  LodgingForCamp toEntity(LodgingForCampDto dto);

  CheckAvailabilityForCampDto toDto(CheckAvailabilityForCamp entity);

  List<CheckAvailabilityForCampDto> toDtos(List<CheckAvailabilityForCamp> entity);

  GuestsForCampDto toDto(GuestsForCamp dto);

  LodgingForCampDto toDto(LodgingForCamp dto);
}
