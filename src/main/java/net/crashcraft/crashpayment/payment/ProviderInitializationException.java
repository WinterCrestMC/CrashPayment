package net.crashcraft.crashpayment.payment;

public class ProviderInitializationException extends Exception{
    public ProviderInitializationException(){
        super("Unable to initialize payment provider");
    }
}
