package com.ecommerce.customer.controller;

import com.ecommerce.library.model.Customer;
import com.ecommerce.library.model.Product;
import com.ecommerce.library.model.ShoppingCart;
import com.ecommerce.library.service.CustomerService;
import com.ecommerce.library.service.ProductService;
import com.ecommerce.library.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import java.util.Optional;

@Controller
public class CartController {
    @Autowired
    private CustomerService customerService;

    @Autowired
    private ShoppingCartService cartService;

    @Autowired
    private ProductService productService;

    @GetMapping("/cart")
    public String cart(Model model, Principal principal, HttpSession session){
        if(principal == null){
            return "redirect:/login";
        }
        String username = principal.getName();
        Customer customer = customerService.findByUsername(username);
        Optional<ShoppingCart> shoppingCart = Optional.ofNullable(customer.getShoppingCart());
        System.out.println("shoppingCart: " + shoppingCart.get());
        if(!shoppingCart.isPresent()){
            model.addAttribute("check", "No item in your cart");
        }else{

            session.setAttribute("totalItems", shoppingCart.map(ShoppingCart::getTotalItems).orElse(0));
            model.addAttribute("grandTotal",  shoppingCart.map(ShoppingCart::getTotalItems).orElse(0));
            model.addAttribute("subTotal", shoppingCart.map(ShoppingCart::getTotalPrices).orElse(0.0));
            model.addAttribute("shoppingCart", shoppingCart);
        }


        return "cart";
    }


    @PostMapping("/add-to-cart")
    public String addItemToCart(
            @RequestParam("id") Long productId,
            @RequestParam(value = "quantity", required = false, defaultValue = "1") int quantity,
            Principal principal,
            HttpServletRequest request){

        if(principal == null){
            return "redirect:/login";
        }
        Product product = productService.getProductById(productId);

        String username = principal.getName();
        Customer customer = customerService.findByUsername(username);

        ShoppingCart cart = cartService.addItemToCart(productService.transferToProductDto(product), quantity, customer.getUsername());
        return "redirect:" + request.getHeader("Referer");

    }


    @RequestMapping(value = "/update-cart", method = RequestMethod.POST, params = "action=update")
    public String updateCart(@RequestParam("quantity") int quantity,
                             @RequestParam("id") Long productId,
                             Model model,
                             Principal principal
    ){

        if(principal == null){
            return "redirect:/login";
        }else{
            String username = principal.getName();
            Customer customer = customerService.findByUsername(username);
            Product product = productService.getProductById(productId);
            ShoppingCart cart = cartService.updateCart(productService.transferToProductDto(product), quantity, customer.getUsername());

            model.addAttribute("shoppingCart", cart);
            return "redirect:/cart";
        }

    }


    @RequestMapping(value = "/update-cart", method = RequestMethod.POST, params = "action=delete")
    public String deleteItemFromCart(@RequestParam("id") Long productId,
                                     Model model,
                                     Principal principal){
        if(principal == null){
            return "redirect:/login";
        }else{
            String username = principal.getName();
            Customer customer = customerService.findByUsername(username);
            Product product = productService.getProductById(productId);
            ShoppingCart cart = cartService.removeItemFromCart(productService.transferToProductDto(product), customer.getUsername());
            model.addAttribute("shoppingCart", cart);
            return "redirect:/cart";
        }

    }



}
