package com.esra.booktracker.controllers;

import java.io.IOException;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.esra.booktracker.models.User;
import com.esra.booktracker.services.UserService;
import com.esra.booktracker.validators.RegistrationValidator;
import com.esra.booktracker.validators.UserValidator;


@Controller
public class UserController {
	@Autowired
	private UserService userService;
	@Autowired
	private UserValidator userValidator;
	@Autowired 
	private RegistrationValidator registrationValidator;

	@GetMapping("/")
	public String welcome( HttpSession session) {
		if(session.getAttribute("user__id") == null) return "redirect:/login";
		return "redirect:/dashboard";
	}
	@GetMapping("/dashboard")
	public String dashboard(Model model, HttpSession session) {
		// Check if there is any active user session.
		if(session.getAttribute("user__id") == null) return "redirect:/";
		User user = this.userService.findOneUser((Long) session.getAttribute("user__id"));
		model.addAttribute("user", user);
		
		return "dashboard.jsp";
	}
	@GetMapping("/register")
	public String getRegister(@ModelAttribute("user") User user) {
		return "registration.jsp";
	}
	
	//Show WishList
	@GetMapping("/wishlist")
	public String showWishList(HttpSession session, Model model) {
		if(session.getAttribute("user__id") == null) return "redirect:/";
		User user = this.userService.findOneUser((Long) session.getAttribute("user__id"));
		model.addAttribute("user", user);
		return"wishlist.jsp";
	}


	/**
	 * User registration
	 * @param user
	 * @param result
	 * @param session
	 * @return
	 */
	@PostMapping("/register")
	public String register(@Valid @ModelAttribute("user") User user, BindingResult result, HttpSession session) {
		System.out.println("User:" + user.toString());
		registrationValidator.validate(user, result);
		if (result.hasErrors()) {
			return "registration.jsp";
		}
		User newUser = this.userService.registerUser(user);
		session.setAttribute("user__id", newUser.getId());
		return "redirect:/dashboard";
	}
	
	@GetMapping("/login")
	public String login(@ModelAttribute("user") User user) {
		return "login.jsp";
	}


	/**
	 * Handle User login
	 * @param session
	 * @param email
	 * @param password
	 * @param redirectAttr
	 * @return
	 */
	@PostMapping("/login")
	public String login(HttpSession session, @RequestParam("loginEmail") String email,
			@RequestParam("loginPassword") String password, RedirectAttributes redirectAttr) {
		if (!this.userService.authenticateUser(email, password)) {
			redirectAttr.addFlashAttribute("loginError", "Invalid Credentials");
			return "redirect:/";
		}
		User userToBeLoggedIn = this.userService.getUserByEmail(email);
		session.setAttribute("user__id", userToBeLoggedIn.getId());
		return "redirect:/dashboard";
	}

	/**
	 * Handle User logout.
	 * @param session
	 * @return
	 */
	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/";
	}
	
	@GetMapping("/profile/{id}")
	public String userProfile(HttpSession session, Model viewModel, @PathVariable("id") Long id) {
		// Check if there is any active user session.
		if(session.getAttribute("user__id") == null) return "redirect:/";
		User user = this.userService.findOneUser(id);
		if(user==null) user = this.userService.findOneUser((Long) session.getAttribute("user__id"));
		viewModel.addAttribute("user", user);
		viewModel.addAttribute("userId", session.getAttribute("user__id"));
		
		return "userprofile.jsp";
	}
	@GetMapping("/user/edit/{id}")
	public String edit(@PathVariable("id") Long id, 
			@ModelAttribute("user") User user, Model model,
			HttpSession session) {
		// Check if there is any active user session.
		if(session.getAttribute("user__id") == null) return "redirect:/";
		User editUser = userService.findOneUser(id);
		// Check if the given id returns anything from DB
		// This will prevent white label error on UI
		if (editUser == null) {
			System.out.println("Idea id=" + id + " is not found in DB.");
			return "redirect:/dashboard";
		}
		// Verify User has access to Edit the User
		if (editUser.getId().compareTo((Long) session.getAttribute("user__id")) != 0)
			return "redirect:/dashboard";
		model.addAttribute("user", editUser);
		return "edituser.jsp";
	}
	@PostMapping("user/edit/{id}")
	public String update(@PathVariable("id") Long id, 
			@ModelAttribute("user") User user,
			BindingResult result, HttpSession session) throws IOException {
		userValidator.validate(user, result);
		// Check if there is any active user session.
		if(session.getAttribute("user__id") == null) return "redirect:/";
		User editUser = userService.findOneUser(id);
		// Check if the given id returns anything from DB
		// This will prevent white label error on UI
		if (editUser == null) {
			System.out.println("Idea id=" + id + " is not found in DB.");
			return "redirect:/dashboard";
		}
		// Verify User has access to Edit the User
		if (editUser.getId().compareTo((Long) session.getAttribute("user__id")) != 0)
			return "redirect:/dashboard";
		
		userService.updateUser(editUser, user);
		if (result.hasErrors()) {
			return "edituser.jsp";
		}
		userService.saveUser(editUser);
		return "redirect:/profile/"+id;
	}
	

}



