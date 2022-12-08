package com.rahul.rai.nframework.client;
import com.rahul.rai.nframework.common.*;
import com.rahul.rai.nframework.common.exceptions.*;
import java.io.*;
import java.net.*;
import java.nio.charset.*;
public class NFrameworkClient
{
public Object execute(String path,Object ...arguments) throws Throwable
{
try
{
Request request=new Request();
request.setServicePath(path);
request.setArguments(arguments);
Socket socket=new Socket("localhost",5500);
OutputStream os=socket.getOutputStream();
InputStream is=socket.getInputStream();
String requestJSONString=JSONUtil.toJSON(request);
byte [] requestBytes=requestJSONString.getBytes(StandardCharsets.UTF_8);
byte [] header=new byte[1024];
byte [] tmp=new byte[1024];
int i,j,k;
int requestLength=requestBytes.length;
int x=requestLength;
j=1023;
while(x>0)
{
header[j]=(byte)(x%10);
x=x/10;
j--;
}
os.write(header,0,1024);
os.flush();

int bytesReadCount;
byte ack[]=new byte[1];
while(true)
{
bytesReadCount=is.read(ack);
if(bytesReadCount==-1) continue;
break;
}
int bytesToSend=requestLength;
j=0;
int chunkSize=1024;
while(j<bytesToSend)
{
if(chunkSize>(bytesToSend-j)) chunkSize=bytesToSend-j;
os.write(requestBytes,j,chunkSize);
os.flush();
j=j+chunkSize;
}

int bytesToReceive=1024;
j=0;
i=0;
while(j<bytesToReceive)
{
bytesReadCount=is.read(tmp);
if(bytesReadCount==-1) continue;
for(k=0;k<bytesReadCount;k++)
{
header[i]=tmp[k];
i++;
}
j=j+bytesReadCount;
}


j=1023;
int responseLength=0;
i=1;
while(j>=0)
{
responseLength=responseLength+(header[j]*i);
i=i*10;
j--;
}

ack[0]=1;
os.write(ack,0,1);
os.flush();



byte[] responseBytes=new byte[responseLength];
bytesToReceive=responseLength;
j=0;
i=0;
while(j<bytesToReceive)
{
bytesReadCount=is.read(tmp);
if(bytesReadCount==-1) continue;
for(k=0;k<bytesReadCount;k++)
{
responseBytes[i]=tmp[k];
i++;
}
j=j+bytesReadCount;
}


ack[0]=1;
os.write(ack,0,1);
os.flush();
socket.close();

String responseJSONString=new String(responseBytes,StandardCharsets.UTF_8);
Response response=JSONUtil.fromJSON(responseJSONString,Response.class);
if(response.getSuccess())
{
return response.getResult();
}
else
{
throw response.getException();
}
}catch(Exception exception)
{
throw new RuntimeException(exception.getMessage());
}
}
}