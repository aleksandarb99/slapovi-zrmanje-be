package com.slapovizrmanje.api.service;

import com.slapovizrmanje.api.dao.CampRequestDao;
import com.slapovizrmanje.api.exception.BadRequestException;
import com.slapovizrmanje.api.exception.NotFoundException;
import com.slapovizrmanje.api.mapper.CampRequestMapper;
import com.slapovizrmanje.api.model.CampRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationService {
  private final CampRequestDao campRequestDao;
//  TODO: Data model update and extract commons subproject
  public void verify(String email, String id) {
    log.info("CAMP REQUEST DAO - Fetching by email and id pair.");
    List<CampRequest> foundEntities = campRequestDao.findByEmailAndIdPair(email, id);
    log.info(String.format("Found camp entities: %s", foundEntities));

    if (foundEntities.isEmpty()) {
      throw new NotFoundException(String.format("Entity with id '%s' does not exist.", id));
    }

    CampRequest request = foundEntities.get(0);

    if (request.isVerified()) {
      throw new BadRequestException(String.format("Entity with id '%s' is already verified.", id));
    }

    log.info(String.format("CAMP REQUEST DAO - Verify email of camp reservation request: %s.", request));

    request.setVerified(true);
    campRequestDao.update(request);
    log.info("Successfully verified!");

//    TODO: Send mail to Admin
  }
}
