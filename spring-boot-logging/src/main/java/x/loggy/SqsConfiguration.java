package x.loggy;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.http.AmazonHttpClient;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import io.smartup.localstack.EnableLocalStack;
import org.springframework.cloud.aws.messaging.config.SimpleMessageListenerContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static x.loggy.ProductionSpies.spyOn;

@Configuration
@EnableLocalStack
public class SqsConfiguration {
    @Bean
    public AWSCredentialsProvider awsCredentialsProvider() {
        return new AWSStaticCredentialsProvider(
                new AnonymousAWSCredentials());
    }

    @Bean
    public SimpleMessageListenerContainerFactory messageListenerContainerFactory(
            final AmazonSQSAsync sqs,
            final LoggingForAmazonHttpClient answer) {
        final AmazonSQSAsyncClient realSQS = spyOn(sqs, "realSQS", false);
        final AmazonHttpClient client = spyOn(realSQS, "client", false);

        doAnswer(answer)
                .when(client).execute(any(), any(), any(), any());

        final var factory = new SimpleMessageListenerContainerFactory();
        factory.setAmazonSqs(sqs);
        return factory;
    }
}
