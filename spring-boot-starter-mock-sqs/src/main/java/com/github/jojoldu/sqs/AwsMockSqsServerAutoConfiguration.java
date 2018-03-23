package com.github.jojoldu.sqs;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.github.jojoldu.sqs.annotation.ConditionalOnMissingMockSqsServer;
import com.github.jojoldu.sqs.annotation.ConditionalOnMockSqs;
import com.github.jojoldu.sqs.annotation.ConditionalOnMockSqsServer;
import com.github.jojoldu.sqs.config.SqsProperties;
import com.github.jojoldu.sqs.config.SqsQueues;
import org.elasticmq.rest.sqs.SQSRestServer;
import org.elasticmq.rest.sqs.SQSRestServerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;

/**
 * Created by jojoldu@gmail.com on 2018. 3. 17.
 * Blog : http://jojoldu.tistory.com
 * Github : https://github.com/jojoldu
 */

@Configuration
@ConditionalOnMockSqs
public class AwsMockSqsServerAutoConfiguration {
    private SqsProperties sqsProperties;
    private SqsQueues sqsQueues;

    public AwsMockSqsServerAutoConfiguration(SqsProperties sqsProperties, SqsQueues sqsQueues) {
        this.sqsProperties = sqsProperties;
        this.sqsQueues = sqsQueues;
    }

    @Bean("amazonSqs")
    @Primary
    @ConditionalOnMissingMockSqsServer
    public AmazonSQSAsync useMockAmazonSqs() {
        return createMockSqsAsync();
    }

    @Bean("amazonSqs")
    @Primary
    @DependsOn("sqsRestServer")
    @ConditionalOnMockSqsServer
    public AmazonSQSAsync createMockAmazonSqs() {
        AmazonSQSAsync sqsAsync = createMockSqsAsync();
        sqsQueues.getQueues().forEach(queueData -> sqsAsync.createQueue(queueData.createQueueRequest()));
        return sqsAsync;
    }

    private AmazonSQSAsync createMockSqsAsync() {
        AmazonSQSAsyncClientBuilder sqsBuilder = AmazonSQSAsyncClientBuilder.standard();
        sqsBuilder.setCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("x", "x")));
        sqsBuilder.setEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(sqsProperties.getEndPoint(), ""));
        return sqsBuilder.build();
    }

    @Bean
    @ConditionalOnMockSqsServer
    public SQSRestServer sqsRestServer() {
        return SQSRestServerBuilder
                .withInterface(sqsProperties.getHost())
                .withPort(sqsProperties.getPort())
                .start();
    }
}