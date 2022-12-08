import com.rahul.rai.nframework.client.*;
public class BankUI
{
public static void main(String gg[])
{
try
{
NFrameworkClient client=new NFrameworkClient();
Object result=client.execute("/Bank/getBranchName",gg[0]);
System.out.println(result);
}catch(Throwable e)
{
System.out.println(e);
}
}
}