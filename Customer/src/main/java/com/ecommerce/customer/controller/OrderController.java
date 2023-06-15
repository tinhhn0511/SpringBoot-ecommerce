package com.ecommerce.customer.controller;

import com.ecommerce.library.model.Customer;
import com.ecommerce.library.model.Order;
import com.ecommerce.library.model.ShoppingCart;
import com.ecommerce.library.service.CustomerService;
import com.ecommerce.library.service.OrderService;
import com.ecommerce.library.service.ShoppingCartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
public class OrderController {

    @Autowired
    private CustomerService customerService;
    @Autowired
    private  OrderService orderService;
    @Autowired
    private ShoppingCartService cartService;

    @GetMapping("/check-out")
    public String checkout(Model model, Principal principal){
        if(principal == null){
            return "redirect:/login";
        }
        String username = principal.getName();
        Customer customer = customerService.findByUsername(username);
        if(customer.getPhoneNumber().trim().isEmpty() || customer.getAddress().trim().isEmpty()
                || customer.getCity().trim().isEmpty() || customer.getCountry().trim().isEmpty()){

            model.addAttribute("customer", customer);
            model.addAttribute("error", "You must fill the information after checkout!");
            return "account";
        }else{
            model.addAttribute("customer", customer);
            Optional<ShoppingCart> shoppingCart = Optional.ofNullable(customer.getShoppingCart());
            model.addAttribute("shoppingCart", shoppingCart);
//            System.out.println(shoppingCart.get().getCartItem());
        }

        return "checkout";
    }


    @GetMapping("/orders")
    public String getOrders(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        } else {
            Customer customer = customerService.findByUsername(principal.getName());
            List<Order> orderList = customer.getOrders();
            System.out.println("List Oder");
            for (Order order : orderList) {
                System.out.println(order.getId());
            }
            model.addAttribute("orders", orderList);
            model.addAttribute("title", "Order");
            model.addAttribute("page", "Order");
            return "order";
        }
    }

    @RequestMapping(value = "/cancel-order", method = {RequestMethod.PUT, RequestMethod.GET})
    public String cancelOrder(Long id, RedirectAttributes attributes) {
        orderService.cancelOrder(id);
        attributes.addFlashAttribute("success", "Cancel order successfully!");
        return "redirect:/orders";
    }


    @RequestMapping(value = "/add-order", method = {RequestMethod.POST})
    public String createOrder(Principal principal,
                              Model model,
                              HttpSession session) {
        if (principal == null) {
            return "redirect:/login";
        } else {
            Customer customer = customerService.findByUsername(principal.getName());
            Optional<ShoppingCart> shoppingCart = Optional.ofNullable(customer.getShoppingCart());
            if(shoppingCart.isPresent()) {
                System.out.println("Create order");
                System.out.println(shoppingCart.get().getId());
                System.out.println("step 1");
                Order order = orderService.save(shoppingCart.get());

                System.out.println("Create order");
                System.out.println(shoppingCart.get().getId());
                System.out.println("step 2");
                session.removeAttribute("totalItems");
                model.addAttribute("order", order);
                model.addAttribute("title", "Order Detail");
                model.addAttribute("page", "Order Detail");
                model.addAttribute("success", "Add order successfully");
            }
            return "redirect:/order-detail";
        }
    }

}


