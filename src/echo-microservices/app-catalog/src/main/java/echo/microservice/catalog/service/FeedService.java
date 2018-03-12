package echo.microservice.catalog.service;

import echo.core.domain.dto.FeedDTO;
import echo.microservice.catalog.repository.FeedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class FeedService {

    @Autowired
    private FeedRepository feedRepository;

    @Transactional(readOnly = true)
    public Optional<FeedDTO> findOne(Long id) {
        throw new UnsupportedOperationException("Not yet implemented"); // TODO
    }

    @Transactional(readOnly = true)
    public Optional<FeedDTO> findOneByEchoId(String echoId) {
        throw new UnsupportedOperationException("Not yet implemented"); // TODO
    }

}
