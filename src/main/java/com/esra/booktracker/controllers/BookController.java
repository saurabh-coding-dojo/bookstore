package com.esra.booktracker.controllers;

import java.io.IOException;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.esra.booktracker.models.Book;
import com.esra.booktracker.models.Rating;
import com.esra.booktracker.models.Review;
import com.esra.booktracker.models.User;
import com.esra.booktracker.services.BookService;
import com.esra.booktracker.services.UserService;
import com.esra.booktracker.validators.BookValidator;


@Controller
@RequestMapping("/books")
public class BookController {

	private static final String PROFILE_PAGE = "bookprofile";
	private static final String WISHLIST_PAGE = "wishlist";
	private static final String COMPLETED_PAGE = "completedlist";

	@Autowired
	private UserService userService;
	@Autowired
	private BookService bookService;
	@Autowired
	private BookValidator bookValidator;

	@GetMapping("/{id}")
	public String bookProfile(@PathVariable("id") Long id, HttpSession session, Model viewModel,
			@ModelAttribute("newRating") Rating rating) {
		// Check if there is any active user session.
		if (session.getAttribute("user__id") == null)
			return "redirect:/";
		User user = this.userService.findOneUser((Long) session.getAttribute("user__id"));
		viewModel.addAttribute("user", user);
		Book book = bookService.getBookById(id);
		if (book != null) {
			viewModel.addAttribute("book", book);
		}
		return "bookprofile.jsp";
	}

	@GetMapping("/create")
	public String newBook(HttpSession session, Model viewModel, @ModelAttribute("book") Book book) {
		// Check if there is any active user session.
		if (session.getAttribute("user__id") == null)
			return "redirect:/";
		User user = this.userService.findOneUser((Long) session.getAttribute("user__id"));
		viewModel.addAttribute("user", user);
//		viewModel.addAttribute("books", this.bookService.allbooks());
		return "newbook.jsp";
	}

	@PostMapping("/create")
	public String addBook(@Valid @ModelAttribute("book") Book book, BindingResult result, HttpSession session,
			Model viewModel) {
		User user = this.userService.findOneUser((Long) session.getAttribute("user__id"));
		bookValidator.validate(book, result);
		if (result.hasErrors()) {
			viewModel.addAttribute("user", user);
			return "newbook.jsp";
		}
		book.setUser(user);
		try {
			this.bookService.create(book);
			return "redirect:/dashboard";
		} catch (Exception e) {
			viewModel.addAttribute("error", "Book is already exist in the database with ISBN= " + book.getIsbn());
		}
		viewModel.addAttribute("user", user);
		return "newbook.jsp";
	}

	@GetMapping("/edit/{id}")
	public String edit(@PathVariable("id") Long id, @ModelAttribute("editBook") Book book, Model model,
			HttpSession session) {
		// Check if there is any active user session.
		if (session.getAttribute("user__id") == null)
			return "redirect:/";
		Book editBook = bookService.getOneBook(id);
		// Check if the given id returns anything from DB
		// This will prevent white label error on UI
		if (editBook == null) {
			System.out.println("book id=" + id + " is not found in DB.");
			return "redirect:/dashboard";
		}
		// Verify User has access to Edit the Book
		if (editBook.getUser().getId().compareTo((Long) session.getAttribute("user__id")) != 0)
			return "redirect:/dashboard";
		model.addAttribute("editBook", editBook);
		User user = this.userService.findOneUser((Long) session.getAttribute("user__id"));
		model.addAttribute("user", user);
		return "editbook.jsp";
	}

	@PostMapping("/update/{id}")
	public String update(@PathVariable("id") Long id, @Valid @ModelAttribute("editBook") Book book,
			BindingResult result, HttpSession session) throws IOException {
		bookValidator.validate(book, result);
		if (result.hasErrors()) {
			return "editbook.jsp";
		}
		bookService.updateBook(id, book);
		return "redirect:/books/"+id;
	}

	@GetMapping("/{id}/delete")
	public String delete(@PathVariable("id") Long id, HttpSession session) {
		// Check if there is any active user session.
		if (session.getAttribute("user__id") == null)
			return "redirect:/";
		Book book = bookService.getOneBook(id);
		if (book.getUser().getId().compareTo((Long) session.getAttribute("user__id")) != 0) {
			System.out.println("Warning: Access denied!");
			return "redirect:/dashboard";
		}

		this.bookService.deleteBook(id);
		return "redirect:/dashboard";
//		return "searchpage.jsp";
	}

	@GetMapping("/search")
	public String search(@RequestParam String term, @RequestParam String method,
			@RequestParam String by,
			HttpSession session, Model viewModel,
			@ModelAttribute("book") Book book) {
		// Check if there is any active user session.
		if (session.getAttribute("user__id") == null)
			return "redirect:/";
		User user = this.userService.findOneUser((Long) session.getAttribute("user__id"));
		viewModel.addAttribute("user", user);
		if (term != null) {
			
			List<Book> books = bookService.searchBook(term);
			
			if (books.size() == 0) {
				viewModel.addAttribute("message", term + " Did not return any result :(");
			} else {
				//SortUtility su = new SortUtility();
				//List<Book> result = null;
				switch(by) {
					case "likes":
						if(method.equals("asc")) {
							Collections.sort(books, new SortLikesAsc());
							//result = su.sortByLikeAsc(books);
						} else {
							Collections.sort(books, new SortLikesDesc());
						}
						break;
					case "ratings":
						if(method.equals("asc")) {
							Collections.sort(books, new SortRatingsAsc());
						} else {
							Collections.sort(books, new SortRatingsDesc());
						}
						break;
					case "reviews":
						if(method.equals("asc")) {
							Collections.sort(books, new SortReviewsAsc());
						} else {
							Collections.sort(books, new SortReviewsDesc());
						}
						break;
					case "wish":
						if(method.equals("asc")) {
							Collections.sort(books, new SortWishAsc());
						} else {
							Collections.sort(books, new SortWishDesc());
						}
						break;
				}
				
				viewModel.addAttribute("books", books);
				viewModel.addAttribute("term", term);
			}
			
		}
		return "searchpage.jsp";
	}

	// Like
	@GetMapping("/{id}/like/{page}")
	public String like(HttpSession session, @PathVariable("id") Long id, @PathVariable("page") String page) {
		// Check if there is any active user session.
		if (session.getAttribute("user__id") == null)
			return "redirect:/";
		User user = this.userService.findOneUser((Long) session.getAttribute("user__id"));
		Book book = this.bookService.getOneBook(id);
		this.bookService.likeBook(user, book);
		return whichPage(page, id);
	}

	// Unlike
	@GetMapping("/{id}/unlike/{page}")
	public String unlike(HttpSession session, @PathVariable("id") Long id, @PathVariable("page") String page) {
		// Check if there is any active user session.
		if (session.getAttribute("user__id") == null)
			return "redirect:/";
		User user = this.userService.findOneUser((Long) session.getAttribute("user__id"));
		Book book = this.bookService.getOneBook(id);
		this.bookService.unlikeBook(user, book);
		return whichPage(page, id);
	}
	// Add WishList

	@GetMapping("/{id}/wish/add/{page}")
	public String addWishList(HttpSession session, @PathVariable("id") Long id, @PathVariable("page") String page) {
		// Check if there is any active user session.
		if (session.getAttribute("user__id") == null)
			return "redirect:/";
		User user = this.userService.findOneUser((Long) session.getAttribute("user__id"));
		Book book = this.bookService.getOneBook(id);
		this.bookService.addWishList(user, book);
		return whichPage(page, id);
	}

	// Remove WishList
	@GetMapping("/{id}/wish/remove/{page}")
	public String removeWhishList(HttpSession session, @PathVariable("id") Long id, @PathVariable("page") String page) {
		// Check if there is any active user session.
		if (session.getAttribute("user__id") == null)
			return "redirect:/";
		User user = this.userService.findOneUser((Long) session.getAttribute("user__id"));
		Book book = this.bookService.getOneBook(id);
		this.bookService.removeWishList(user, book);
		return whichPage(page, id);
	}

	// Complete Reading a Book
	@GetMapping("/complete")
	public String completeBook(HttpSession session, @RequestParam(required = false) String isbn,
			@RequestParam(required = false) String date, Model model) {
		// Check if there is any active user session.
		if (session.getAttribute("user__id") == null)
			return "redirect:/";
		User user = this.userService.findOneUser((Long) session.getAttribute("user__id"));
		model.addAttribute("user", user);
		if (isbn != null) {
			Book book = this.bookService.searchBookByIsbn(isbn);
			if (book == null || book.getTitle().isEmpty()) {
				model.addAttribute("error", "Book can not be found in database");
			} else {
				if (!book.getCompletedList().contains(user)) {
					this.bookService.completeBookRead(user, book);
				} else {
					model.addAttribute("error", "You have already read this book!");
				}
			}
			if (book != null)
				return "redirect:/dashboard";
		}
			return "completedbook.jsp";
	}

	// Show Completed Book List
	@GetMapping("/completedbook")
	public String completedBookList(HttpSession session, Model viewModel, @ModelAttribute("book") Book book) {
		// Check if there is any active user session.
		if (session.getAttribute("user__id") == null)
			return "redirect:/";
		User user = this.userService.findOneUser((Long) session.getAttribute("user__id"));
		viewModel.addAttribute("user", user);
		return "completedbooklist.jsp";

	}

	// Rating
	@PostMapping("/addrating")
	public String addRating(HttpSession session, @ModelAttribute("newRating") Rating rating) {
		if (session.getAttribute("user__id") == null)
			return "redirect:/";
		bookService.AddRating(rating);
		return "redirect:/books/" + rating.getRatedBook().getId();

	}

	// Add Review
	@GetMapping("/addreview")
	public String addReview(HttpSession session, @ModelAttribute("newReview") Review review,
			@RequestParam(required = false) Long book_id, Model model) {
		if (session.getAttribute("user__id") == null)
			return "redirect:/";
		if (book_id != null) {
			User user = this.userService.findOneUser((Long) session.getAttribute("user__id"));
			Book book = bookService.getBookById(book_id);
			
			if (book != null) {
				model.addAttribute("book", book);
			}
			model.addAttribute("user", user);
			
			/*
			if(book.getReviews() != null)
			for (Review rev : book.getReviews()) {
				if (rev.getReviewedBy().getId().compareTo(user.getId()) == 0) {
					model.addAttribute("newReview", rev);
					break;
				}
			}
			 */
			return "addreview.jsp";
		}
		return "redirect:/dashboard";
	}

	// Add Review
	@PostMapping("/submitreview")
	public String submitReview(HttpSession session, @ModelAttribute("newReview") Review review,
			@RequestParam(required = false) Long book_id, Model model) {
		if (session.getAttribute("user__id") == null)
			return "redirect:/";
		bookService.AddReview(review);

		return "redirect:/books/" + review.getReviewedBook().getId();

	}

	// Delete Review /books/${book.id}/review/${review.id}/delete/bookprofile
	@GetMapping("/{id}/review/{rid}/delete/{page}")
	public String deletereview(HttpSession session, @PathVariable("id") Long bookId, @PathVariable("rid") Long id,
			@PathVariable("page") String page) {
		// Check if there is any active user session.
		if (session.getAttribute("user__id") == null)
			return "redirect:/";
		Review review = this.bookService.findReviewById(id);
		this.bookService.deleteReview(review);
		return whichPage(page, bookId);
	}

	private String whichPage(String page, Long id) {
		switch (page) {
		case PROFILE_PAGE:
			return "redirect:/books/" + id;
		case WISHLIST_PAGE:
			return "redirect:/wishlist";
		case COMPLETED_PAGE:
			return "redirect:/books/completedbook";

		}
		return "redirect:/";
	}
	
	@GetMapping("/{id}/reviews")
	public String getAllReviews(HttpSession session, @PathVariable("id") Long bookId, Model model)
	{
		if (session.getAttribute("user__id") == null)
			return "redirect:/";
		User user = this.userService.findOneUser((Long) session.getAttribute("user__id"));
		model.addAttribute("user", user);
		if (bookId != null) {
			Book book = bookService.getBookById(bookId);
			if (book != null) {
				model.addAttribute("book", book);
			}
		}
		
		return "bookreviews.jsp";
	}


}

class SortLikesAsc implements Comparator<Book>
{
    // Used for sorting in ascending order of
    // roll number
    public int compare(Book a, Book b)
    {
    	System.out.println("a="+a.getLikers().size());
    	System.out.println("b="+b.getLikers().size());
        return a.getLikers().size() - b.getLikers().size();
    }
}

class SortLikesDesc implements Comparator<Book>
{
    // Used for sorting in ascending order of
    // roll number
    public int compare(Book a, Book b)
    {
        return  b.getLikers().size() - a.getLikers().size();
    }
}


class SortReviewsAsc implements Comparator<Book>
{
    // Used for sorting in ascending order of
    // roll number
    public int compare(Book a, Book b)
    {
        return a.getReviews().size() - b.getReviews().size();
    }
}

class SortReviewsDesc implements Comparator<Book>
{
    // Used for sorting in ascending order of
    // roll number
    public int compare(Book a, Book b)
    {
        return  b.getReviews().size() - a.getReviews().size();
    }
}

class SortRatingsAsc implements Comparator<Book>
{
    // Used for sorting in ascending order of
    // roll number
    public int compare(Book a, Book b)
    {
        return a.getRatings().size() - b.getRatings().size();
    }
}

class SortRatingsDesc implements Comparator<Book>
{
    // Used for sorting in ascending order of
    // roll number
    public int compare(Book a, Book b)
    {
        return  b.getRatings().size() - a.getRatings().size();
    }
}


class SortWishAsc implements Comparator<Book>
{
    // Used for sorting in ascending order of
    // roll number
    public int compare(Book a, Book b)
    {
        return a.getWishList().size() - b.getWishList().size();
    }
}

class SortWishDesc implements Comparator<Book>
{
    // Used for sorting in ascending order of
    // roll number
    public int compare(Book a, Book b)
    {
        return  b.getWishList().size() - a.getWishList().size();
    }
}

