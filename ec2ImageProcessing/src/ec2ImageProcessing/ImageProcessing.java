package ec2ImageProcessing;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.imageio.ImageIO;

public class ImageProcessing 
{
	public void getImgFromSQS(String SQSname) throws Exception
	{
		AmazonSQS queue;
		   
	   	AWSCredentialsProvider credentialsProvider = new ClasspathPropertiesFileCredentialsProvider();
	   	queue = new AmazonSQSClient(credentialsProvider);
	   
	   	String QueueUrl = "https://sqs.us-west-2.amazonaws.com/983680736795/janiszewska1SQS";

	   	while(true)
	   	{
	   		List <com.amazonaws.services.sqs.model.Message> msg = 
	   				queue.receiveMessage(new ReceiveMessageRequest(QueueUrl).withMaxNumberOfMessages(1)).getMessages();
	   		System.out.println("Size:"+msg.size());
	   		if (msg.size() > 0)
	   		{
	   			com.amazonaws.services.sqs.model.Message message = msg.get(0);		
	   			convert(message.getBody());
	   			queue.deleteMessage(new DeleteMessageRequest(QueueUrl, message.getReceiptHandle()));
	   		}
	   		else
	   		{
	   			Thread.sleep(10000);
	   		}
	   	}
	}

	private void convert(String msg) throws Exception 
	{
		String s3 = "aws.ajan";
		BufferedImage image = GetImage(s3, msg);
		image = ImageConverter.Sepia(image);
		Save(image, s3, "new_"+msg);
	}

	private void Save(BufferedImage img, String bucket, String name) throws Exception 
	{
		File tmp = new File("Tmp");
		ImageIO.write(img, "jpg", tmp);
		
		AmazonS3 s3;
		AWSCredentialsProvider credentialsProvider = new ClasspathPropertiesFileCredentialsProvider();
        s3 = new AmazonS3Client(credentialsProvider);
        PutObjectRequest newImg = new PutObjectRequest(bucket, name, tmp);
        newImg.setCannedAcl(CannedAccessControlList.PublicRead); 
        s3.putObject(newImg);
	}

	private BufferedImage GetImage(String bucket, String name) throws Exception 
	{
		AmazonS3 s3;
		AWSCredentialsProvider credentialsProvider = new ClasspathPropertiesFileCredentialsProvider();
        s3 = new AmazonS3Client(credentialsProvider);
        S3Object object = s3.getObject(bucket, name);
        
        File file_temp = new File("tmp.jpg");
        if (!file_temp.exists())
        {
        	file_temp.createNewFile();
        }
        
        InputStream in = object.getObjectContent();
		byte[] buf = new byte[1024];
		OutputStream out = new FileOutputStream(file_temp);
		int count;
		while ((count = in.read(buf)) != -1) 
		{
			if (Thread.interrupted()) 
			{
				throw new InterruptedException();
			}
			out.write(buf, 0, count);
		}
		out.close();
		in.close();

		File image = file_temp;
		BufferedImage in2 = ImageIO.read(image);
		return in2;
	}
}
