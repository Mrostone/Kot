package kot.utils;

public class Regs {
    public static String getReg(int ind)
    {
        if(ind == 0)
        {
            return "%rdi";
        }
        else if(ind == 1)
        {
            return "%rsi";
        }
        else if(ind == 2)
        {
            return "%rdx";
        }
        else if(ind == 3)
        {
            return "%rcx";
        }
        else if(ind == 4)
        {
            return "%r8";
        }
        return "";
    }
}