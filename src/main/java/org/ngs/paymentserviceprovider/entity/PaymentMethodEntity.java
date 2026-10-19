package org.ngs.paymentserviceprovider.entity;

import jakarta.persistence.*;
import lombok.*;
import org.ngs.paymentserviceprovider.enums.PaymentMethodType;

@Entity
@Table(name = "payment_methods")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodEntity extends BaseEntity {
    @Column(nullable = false, unique = true, length = 64)
    private String uniqueId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethodType type;

    @Column(name = "card_number", nullable = false, length = 19)
    private String cardNumber;

    @Column(nullable = false, length = 20)
    private String brand;

    @Column(nullable = false, length = 4)
    private String last4;

    @Column(name = "exp_month", nullable = false)
    private Integer expMonth;

    @Column(name = "exp_year", nullable = false)
    private Integer expYear;

    private String holder;
}