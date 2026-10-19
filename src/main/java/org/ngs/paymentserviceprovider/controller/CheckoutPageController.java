package org.ngs.paymentserviceprovider.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@Controller
public class CheckoutPageController {

    @GetMapping("/checkout/{id}")
    public String checkoutPage(@PathVariable String id) {
        return "forward:/checkout.html";
    }
}
