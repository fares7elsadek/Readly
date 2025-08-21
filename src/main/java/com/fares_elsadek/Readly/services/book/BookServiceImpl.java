package com.fares_elsadek.Readly.services.book;

import com.fares_elsadek.Readly.dtos.ApiResponse;
import com.fares_elsadek.Readly.dtos.BookHistoryDto;
import com.fares_elsadek.Readly.dtos.BookRequestDto;
import com.fares_elsadek.Readly.dtos.BookResponseDto;
import com.fares_elsadek.Readly.entity.BookTransaction;
import com.fares_elsadek.Readly.exceptions.AccessDeniedException;
import com.fares_elsadek.Readly.exceptions.InvalidTokenException;
import com.fares_elsadek.Readly.exceptions.NotFoundException;
import com.fares_elsadek.Readly.mapper.BookHistoryMapper;
import com.fares_elsadek.Readly.mapper.BookMapper;
import com.fares_elsadek.Readly.repository.BookHistoryRepository;
import com.fares_elsadek.Readly.repository.BookRepository;
import com.fares_elsadek.Readly.repository.UserRepository;
import com.fares_elsadek.Readly.services.uploadfiles.UploadFilesService;
import com.fares_elsadek.Readly.utils.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService{

    private final BookRepository bookRepository;
    private final BookHistoryRepository bookHistoryRepository;
    private final BookMapper bookMapper;
    private final UserRepository userRepository;
    private final BookHistoryMapper bookHistoryMapper;
    private final UploadFilesService uploadFilesService;

    @Override
    public ApiResponse<BookResponseDto> saveBook(BookRequestDto bookRequest) {
        try {
            var entity = bookMapper.toEntity(bookRequest);
            entity.setOwner(userRepository.findById(getUserId()).orElseThrow(
                    () -> new NotFoundException("User", getUserId())
            ));
            var savedBook = bookRepository.save(entity);

            if(StringUtils.hasText(savedBook.getId())){
                var dto = bookMapper.toBookResponse(savedBook);
                return ApiResponse.success("Book has been successfully created and saved to your library.", dto);
            } else {
                return ApiResponse.error("Failed to create book. Please verify your information and try again.");
            }
        } catch (Exception ex) {
            log.error("Error occurred while saving book: {}", ex.getMessage(), ex);
            return ApiResponse.error("An unexpected error occurred while creating the book. Please try again later.");
        }
    }

    @Override
    public ApiResponse<BookResponseDto> getBookById(String bookId) {
        try {
            var entity = bookRepository.findById(bookId).orElseThrow(
                    () -> new NotFoundException("Book", bookId)
            );

            var dto = bookMapper.toBookResponse(entity);
            return ApiResponse.success("Book details retrieved successfully.", dto);
        } catch (NotFoundException ex) {
            return ApiResponse.error("The requested book could not be found. It may have been removed or the ID is incorrect.");
        } catch (Exception ex) {
            log.error("Error occurred while retrieving book with ID {}: {}", bookId, ex.getMessage(), ex);
            return ApiResponse.error("An unexpected error occurred while retrieving the book details. Please try again later.");
        }
    }

    @Override
    public ApiResponse<List<BookResponseDto>> getAllBooks(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            var entities = bookRepository.findAll(pageable);

            List<BookResponseDto> bookDtos = entities.getContent().stream()
                    .map(bookMapper::toBookResponse)
                    .collect(Collectors.toList());

            String message = bookDtos.isEmpty() ?
                    "No books are currently available in the library." :
                    String.format("Successfully retrieved %d books from the library (page %d of %d).",
                            bookDtos.size(), page + 1, entities.getTotalPages());

            return ApiResponse.success(message, bookDtos);
        } catch (Exception ex) {
            log.error("Error occurred while retrieving all books: {}", ex.getMessage(), ex);
            return ApiResponse.error("An unexpected error occurred while retrieving the book collection. Please try again later.");
        }
    }

    @Override
    public ApiResponse<List<BookResponseDto>> findAllByOwner(int page, int size) {
        try {
            var userId = getUserId();
            Pageable pageable = PageRequest.of(page, size);
            var entities = bookRepository.findAllByCreatedByEquals(userId, pageable);

            List<BookResponseDto> bookDtos = entities.getContent().stream()
                    .map(bookMapper::toBookResponse)
                    .collect(Collectors.toList());

            String message = bookDtos.isEmpty() ?
                    "You haven't added any books to your library yet. Start building your collection!" :
                    String.format("Successfully retrieved %d of your books (page %d of %d).",
                            bookDtos.size(), page + 1, entities.getTotalPages());

            return ApiResponse.success(message, bookDtos);
        } catch (InvalidTokenException ex) {
            return ApiResponse.error("Your session has expired. Please log in again to access your books.");
        } catch (Exception ex) {
            log.error("Error occurred while retrieving books by owner: {}", ex.getMessage(), ex);
            return ApiResponse.error("An unexpected error occurred while retrieving your books. Please try again later.");
        }
    }

    @Override
    public ApiResponse<List<BookResponseDto>> findAllBorrowedBooks(int page, int size) {
        try {
            var userId = getUserId();
            Pageable pageable = PageRequest.of(page, size);
            var entities = bookHistoryRepository.findAllBorrowedBooks(userId, pageable);

            List<BookResponseDto> bookDtos = entities.getContent().stream()
                    .map(transaction -> bookMapper.toBookResponse(transaction.getBook()))
                    .collect(Collectors.toList());

            String message = bookDtos.isEmpty() ?
                    "You haven't borrowed any books yet. Explore our library to find books to borrow!" :
                    String.format("Successfully retrieved %d of your borrowed books (page %d of %d).",
                            bookDtos.size(), page + 1, entities.getTotalPages());

            return ApiResponse.success(message, bookDtos);
        } catch (InvalidTokenException ex) {
            return ApiResponse.error("Your session has expired. Please log in again to access your borrowed books.");
        } catch (Exception ex) {
            log.error("Error occurred while retrieving borrowed books: {}", ex.getMessage(), ex);
            return ApiResponse.error("An unexpected error occurred while retrieving your borrowed books. Please try again later.");
        }
    }

    @Override
    public ApiResponse<BookResponseDto> updateShareableStatus(String bookId) {
        try {
            var userId = getUserId();
            var entity = bookRepository.findById(bookId).orElseThrow(
                    () -> new NotFoundException("Book", bookId)
            );

            if(!entity.getOwner().getId().equals(userId))
                throw new AccessDeniedException("You can only modify the sharing settings of books that you own.");

            boolean newShareableStatus = !entity.isShareable();
            entity.setShareable(newShareableStatus);
            var book = bookRepository.save(entity);
            var dto = bookMapper.toBookResponse(book);

            String statusMessage = newShareableStatus ? "available for sharing" : "no longer available for sharing";
            return ApiResponse.success(String.format("Book sharing status updated successfully. This book is now %s.", statusMessage), dto);

        } catch (NotFoundException ex) {
            return ApiResponse.error("The book you're trying to modify could not be found. It may have been removed.");
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (InvalidTokenException ex) {
            return ApiResponse.error("Your session has expired. Please log in again to modify book settings.");
        } catch (Exception ex) {
            log.error("Error occurred while updating shareable status for book {}: {}", bookId, ex.getMessage(), ex);
            return ApiResponse.error("An unexpected error occurred while updating the book's sharing status. Please try again later.");
        }
    }

    @Override
    public ApiResponse<BookResponseDto> updateArchivedStatus(String bookId) {
        try {
            var userId = getUserId();
            var entity = bookRepository.findById(bookId).orElseThrow(
                    () -> new NotFoundException("Book", bookId)
            );

            if(!entity.getOwner().getId().equals(userId))
                throw new AccessDeniedException("You can only archive or unarchive books that you own.");

            boolean newArchivedStatus = !entity.isArchived();
            entity.setArchived(newArchivedStatus);
            var book = bookRepository.save(entity);
            var dto = bookMapper.toBookResponse(book);

            String statusMessage = newArchivedStatus ? "archived" : "restored from archive";
            return ApiResponse.success(String.format("Book has been successfully %s.", statusMessage), dto);

        } catch (NotFoundException ex) {
            return ApiResponse.error("The book you're trying to modify could not be found. It may have been removed.");
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (InvalidTokenException ex) {
            return ApiResponse.error("Your session has expired. Please log in again to modify book settings.");
        } catch (Exception ex) {
            log.error("Error occurred while updating archived status for book {}: {}", bookId, ex.getMessage(), ex);
            return ApiResponse.error("An unexpected error occurred while updating the book's archive status. Please try again later.");
        }
    }

    @Transactional
    @Override
    public ApiResponse<BookHistoryDto> borrowBook(String bookId) {
        try {
            var userId = getUserId();
            var entity = bookRepository.findById(bookId).orElseThrow(
                    () -> new NotFoundException("Book", bookId)
            );

            if(entity.getOwner().getId().equals(userId))
                throw new AccessDeniedException("You cannot borrow your own books. This book is already in your personal library.");

            if(entity.isArchived() || !entity.isShareable())
                throw new AccessDeniedException("This book is currently unavailable for borrowing. It may be archived or not shared by the owner.");

            if(bookHistoryRepository.isAlreadyBorrowedByCurrentUser(userId, bookId))
                throw new AccessDeniedException("You have already borrowed this book. Please return it before borrowing again.");

            if(bookHistoryRepository.isAlreadyBorrowed(bookId))
                throw new AccessDeniedException("This book is currently borrowed by another user. Please try again later when it becomes available.");

            var user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User", userId));

            var historyBuild = BookTransaction.builder()
                    .book(entity)
                    .user(user)
                    .returnApproved(false)
                    .returned(false)
                    .build();

            var bookTransaction = bookHistoryRepository.save(historyBuild);
            var dto = bookHistoryMapper.toDto(bookTransaction);

            return ApiResponse.success("Book borrowed successfully! You can now access this book in your borrowed collection.", dto);

        } catch (NotFoundException ex) {
            return ApiResponse.error("The book you're trying to borrow could not be found. It may have been removed or is no longer available.");
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (InvalidTokenException ex) {
            return ApiResponse.error("Your session has expired. Please log in again to borrow books.");
        } catch (Exception ex) {
            log.error("Error occurred while borrowing book {}: {}", bookId, ex.getMessage(), ex);
            return ApiResponse.error("An unexpected error occurred while borrowing the book. Please try again later.");
        }
    }

    @Override
    public ApiResponse<BookHistoryDto> returnBorrowBook(String bookId) {
        try {
            var userId = getUserId();
            var entity = bookRepository.findById(bookId).orElseThrow(
                    () -> new NotFoundException("Book", bookId)
            );

            if(!bookHistoryRepository.isAlreadyBorrowedByCurrentUser(userId, bookId))
                throw new AccessDeniedException("You cannot return a book that you haven't borrowed or have already returned.");

            var history = bookHistoryRepository.findByBookIdAndUserId(userId, bookId)
                    .orElseThrow(() -> new NotFoundException("Book transaction", bookId));

            history.setReturned(true);
            var bookHistory = bookHistoryRepository.save(history);
            var dto = bookHistoryMapper.toDto(bookHistory);

            return ApiResponse.success("Book return request submitted successfully! The book owner will be notified to approve the return.", dto);

        } catch (NotFoundException ex) {
            return ApiResponse.error("The book or transaction record could not be found. The book may have already been returned.");
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (InvalidTokenException ex) {
            return ApiResponse.error("Your session has expired. Please log in again to return books.");
        } catch (Exception ex) {
            log.error("Error occurred while returning book {}: {}", bookId, ex.getMessage(), ex);
            return ApiResponse.error("An unexpected error occurred while processing the book return. Please try again later.");
        }
    }

    @Override
    public ApiResponse<BookHistoryDto> approveReturnBorrowBook(String bookId) {
        try {
            var userId = getUserId();
            var entity = bookRepository.findById(bookId).orElseThrow(
                    () -> new NotFoundException("Book", bookId)
            );

            if(!entity.getOwner().getId().equals(userId))
                throw new AccessDeniedException("You can only approve returns for books that you own.");

            var history = bookHistoryRepository.findByBookIdAndUserId(userId, bookId)
                    .orElseThrow(() -> new NotFoundException("Book transaction", bookId));

            if(!history.isReturned())
                throw new AccessDeniedException("Cannot approve return for a book that hasn't been marked as returned by the borrower.");

            history.setReturnApproved(true);
            var bookHistory = bookHistoryRepository.save(history);
            var dto = bookHistoryMapper.toDto(bookHistory);

            return ApiResponse.success("Book return has been approved successfully! The book is now available for borrowing again.", dto);

        } catch (NotFoundException ex) {
            return ApiResponse.error("The book or transaction record could not be found. The return may have already been processed.");
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (InvalidTokenException ex) {
            return ApiResponse.error("Your session has expired. Please log in again to approve book returns.");
        } catch (Exception ex) {
            log.error("Error occurred while approving return for book {}: {}", bookId, ex.getMessage(), ex);
            return ApiResponse.error("An unexpected error occurred while approving the book return. Please try again later.");
        }
    }

    @Override
    public ApiResponse<BookResponseDto> uploadBookCoverPicture(String bookId, MultipartFile coverImage) {
        try {
            var userId = getUserId();
            var entity = bookRepository.findById(bookId).orElseThrow(
                    () -> new NotFoundException("Book", bookId)
            );

            if(!entity.getOwner().getId().equals(userId))
                throw new AccessDeniedException("You can only upload cover images for books that you own.");

            if(coverImage == null || coverImage.isEmpty())
                return ApiResponse.error("Please select a valid image file to upload as the book cover.");

            var filename = uploadFilesService.saveFile(coverImage);
            if(!StringUtils.hasText(filename))
                return ApiResponse.error("Failed to save the uploaded image. Please ensure the file is a valid image format and try again.");

            entity.setBookCover(filename);
            var book = bookRepository.save(entity);
            var dto = bookMapper.toBookResponse(book);

            return ApiResponse.success("Book cover image has been uploaded and updated successfully!", dto);

        } catch (NotFoundException ex) {
            return ApiResponse.error("The book you're trying to update could not be found. It may have been removed.");
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (InvalidTokenException ex) {
            return ApiResponse.error("Your session has expired. Please log in again to upload book covers.");
        } catch (Exception ex) {
            log.error("Error occurred while uploading cover for book {}: {}", bookId, ex.getMessage(), ex);
            return ApiResponse.error("An unexpected error occurred while uploading the book cover. Please try again later.");
        }
    }

    private String getUserId(){
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        var userId = principal.userId();
        if(!StringUtils.hasText(userId))
            throw new InvalidTokenException("Authentication token is invalid or expired. Please log in again.");
        return userId;
    }
}