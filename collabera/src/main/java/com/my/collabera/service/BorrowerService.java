package com.my.collabera.service;

import com.my.collabera.dto.BorrowerRequest;
import com.my.collabera.dto.BorrowerResponse;
import com.my.collabera.exception.InvalidDataException;
import com.my.collabera.model.Borrower;
import com.my.collabera.repository.BorrowerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BorrowerService {
    private final BorrowerRepository borrowerRepository;

    @Transactional
    public BorrowerResponse registerBorrower(BorrowerRequest request) {
        if (borrowerRepository.existsByEmail(request.getEmail())) {
            throw new InvalidDataException("Email already registered");
        }

        Borrower borrower = new Borrower();
        borrower.setName(request.getName());
        borrower.setEmail(request.getEmail());

        Borrower saved = borrowerRepository.save(borrower);
        return new BorrowerResponse(
                saved.getId(),
                saved.getName(),
                saved.getEmail(),
                saved.getCreatedAt()
        );
    }
}
