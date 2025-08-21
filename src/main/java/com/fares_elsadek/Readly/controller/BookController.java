package com.fares_elsadek.Readly.controller;

import com.fares_elsadek.Readly.dtos.ApiResponse;
import com.fares_elsadek.Readly.dtos.BookHistoryDto;
import com.fares_elsadek.Readly.dtos.BookRequestDto;
import com.fares_elsadek.Readly.dtos.BookResponseDto;
import com.fares_elsadek.Readly.services.book.BookService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/book")
@RequiredArgsConstructor
@Tag(name = "Book Controller")
@SecurityRequirement(name = "bearerAuth")
public class BookController {

    private final BookService bookService;
    @PostMapping
    public ResponseEntity<ApiResponse<BookResponseDto>> saveBook(@RequestBody @Valid BookRequestDto bookRequest){
        return ResponseEntity.ok(bookService.saveBook(bookRequest));
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<ApiResponse<BookResponseDto>> getBookById(@PathVariable String bookId){
        return ResponseEntity.ok(bookService.getBookById(bookId));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BookResponseDto>>> getAllBooks(
            @RequestParam(name = "page",defaultValue = "0" , required = false) int page,
            @RequestParam(name = "size",defaultValue = "10" , required = false) int size
    ){
        return ResponseEntity.ok(bookService.getAllBooks(page,size));
    }

    @GetMapping("/owner")
    public ResponseEntity<ApiResponse<List<BookResponseDto>>> findAllByOwner(
            @RequestParam(name = "page",defaultValue = "0" , required = false) int page,
            @RequestParam(name = "size",defaultValue = "10" , required = false) int size){
        return ResponseEntity.ok(bookService.findAllByOwner(page,size));
    }

    @GetMapping("/borrowed")
    public ResponseEntity<ApiResponse<List<BookResponseDto>>> findAllBorrowedBooks(
            @RequestParam(name = "page",defaultValue = "0" , required = false) int page,
            @RequestParam(name = "size",defaultValue = "10" , required = false) int size){
        return ResponseEntity.ok(bookService.findAllBorrowedBooks(page,size));
    }

    @PatchMapping("/shareable/{bookId}")
    public ResponseEntity<ApiResponse<BookResponseDto>> updateShareableStatus(
            @PathVariable String bookId){
        return ResponseEntity.ok(bookService.updateArchivedStatus(bookId));
    }

    @PatchMapping("/archived/{bookId}")
    public ResponseEntity<ApiResponse<BookResponseDto>> updateArchivedStatus(
            @PathVariable String bookId){
        return  ResponseEntity.ok(bookService.updateArchivedStatus(bookId));
    }

    @PostMapping("/borrow/{bookId}")
    public ResponseEntity<ApiResponse<BookHistoryDto>> borrowBook(
            @PathVariable String bookId){
        return  ResponseEntity.ok(bookService.borrowBook(bookId));
    }

    @PatchMapping("/borrow/return/{bookId}")
    public ResponseEntity<ApiResponse<BookHistoryDto>> returnBorrowBook(
            @PathVariable String bookId){
        return  ResponseEntity.ok(bookService.returnBorrowBook(bookId));
    }

    @PatchMapping("/borrow/return/approve/{bookId}")
    public ResponseEntity<ApiResponse<BookHistoryDto>> approveReturnBorrowBook(
            @PathVariable String bookId){
        return  ResponseEntity.ok(bookService.approveReturnBorrowBook(bookId));
    }

    @PostMapping(value = "/cover/{bookId}", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<BookResponseDto>> uploadBookCoverPicture(@PathVariable String bookId,
                                                    @RequestParam("cover") MultipartFile cover){
        return  ResponseEntity.ok(bookService.uploadBookCoverPicture(bookId,cover));
    }

}
