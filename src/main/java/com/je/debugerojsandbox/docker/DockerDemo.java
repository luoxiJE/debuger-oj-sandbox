package com.je.debugerojsandbox.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PingCmd;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.core.DockerClientBuilder;

import java.io.IOException;

public class DockerDemo {
    public static void main(String[] args) throws IOException, InterruptedException {
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        // 拉取docker镜像
 /*       String image = "nginx:latest";
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
        PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
            @Override
            public void onNext(PullResponseItem item) {
                // 处理拉取镜像的进度
                System.out.println("下载镜像：" + item.getStatus());
                super.onNext(item);
            }
        };
        pullImageCmd.exec(pullImageResultCallback).awaitCompletion();
*/

        // 删除docker镜像
        String image = "nginx:latest";
        dockerClient.removeImageCmd(image).exec();
        dockerClient.close();
    }
}
