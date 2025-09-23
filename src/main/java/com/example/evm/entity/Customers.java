package dealermanagementsystem.project.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
@Entity
public class Customers {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private int customerID;
private String fullname;
private String phone;
private String email;
private String address;
private String nationalID;
public int getCustomerID() {
    return customerID;
}
public void setCustomerID(int customerID) {
    this.customerID = customerID;
}
public String getFullname() {
    return fullname;
}
public void setFullname(String fullname) {
    this.fullname = fullname;
}
public String getPhone() {
    return phone;
}
public void setPhone(String phone) {
    this.phone = phone;
}
public String getEmail() {
    return email;
}
public void setEmail(String email) {
    this.email = email;
}
public String getAddress() {
    return address;
}
public void setAddress(String address) {
    this.address = address;
}
public String getNationalID() {
    return nationalID;
}
public void setNationalID(String nationalID) {
    this.nationalID = nationalID;
}


}
