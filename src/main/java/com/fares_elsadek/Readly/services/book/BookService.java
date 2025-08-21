package com.fares_elsadek.Readly.services.book;

import com.fares_elsadek.Readly.dtos.ApiResponse;
import com.fares_elsadek.Readly.dtos.BookHistoryDto;
import com.fares_elsadek.Readly.dtos.BookRequestDto;
import com.fares_elsadek.Readly.dtos.BookResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BookService {
    public ApiResponse<BookResponseDto> saveBook(BookRequestDto bookRequest);
    public ApiResponse<BookResponseDto> getBookById(String bookId);
    public ApiResponse<List<BookResponseDto>> getAllBooks(int page,int size);
    public ApiResponse<List<BookResponseDto>> findAllByOwner(int page,int size);
    public ApiResponse<List<BookResponseDto>> findAllBorrowedBooks(int page,int size);
    public ApiResponse<BookResponseDto> updateShareableStatus(String bookId);
    public  ApiResponse<BookResponseDto> updateArchivedStatus(String bookId);
    public ApiResponse<BookHistoryDto> borrowBook(String bookId);
    public ApiResponse<BookHistoryDto> returnBorrowBook(String bookId);
    public ApiResponse<BookHistoryDto> approveReturnBorrowBook(String bookId);
    public ApiResponse<BookResponseDto> uploadBookCoverPicture(String bookId, MultipartFile coverImage);
}
