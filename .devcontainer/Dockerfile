FROM quay.io/mhildenb/dev-demo-base:1.0

USER root

# install stern
RUN cd /usr/local/bin && \
    wget https://github.com/wercker/stern/releases/download/1.11.0/stern_linux_amd64 && \
    mv stern_linux_amd64 stern && chmod 755 stern && \
# instal knative client
    wget -qO- https://mirror.openshift.com/pub/openshift-v4/clients/serverless/0.13.1/kn-linux-amd64-0.13.1.tar.gz | tar -zxvf - -C /usr/local/bin ./kn && chmod 755 /usr/local/bin/kn && \
# instal hey
    wget https://storage.googleapis.com/hey-release/hey_linux_amd64 && \
    mv hey_linux_amd64 hey && chmod 755 hey && \
# install istioctl
    curl -L https://github.com/istio/istio/releases/download/1.4.0/istioctl-1.4.0-linux.tar.gz | tar -zxvf - -C /usr/local/bin && chmod 755 /usr/local/bin/istioctl

# install AWS cli
RUN curl "https://d1vvhvl2y92vvt.cloudfront.net/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" && \
    unzip awscliv2.zip && ./aws/install && rm awscliv2.zip && rm -rf ./aws 
    # && ln -s /usr/local/bin/aws2 /usr/local/bin/aws

# for htpasswd support 
# install a more modern version of tkn
RUN dnf install -y httpd-tools \
    && curl -L https://github.com/tektoncd/cli/releases/download/v0.11.0/tkn_0.11.0_Linux_x86_64.tar.gz | \
    tar -xvzf - -C /usr/local/bin/ tkn && chmod 755 /usr/local/bin/tkn

# overwrite existing oc with the absolute newest version of the openshift client
RUN curl -L https://mirror.openshift.com/pub/openshift-v4/clients/ocp/latest/openshift-client-linux.tar.gz | \
    tar -xvzf - -C /usr/bin/ oc && chmod 755 /usr/bin/oc

# graalvm for native compilation
RUN curl -L https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-20.2.0/graalvm-ce-java11-linux-amd64-20.2.0.tar.gz | \
    tar -zxvf - -C /usr/local/ && /usr/local/graalvm-ce-java11-20.2.0/bin/gu install native-image && \
    export GRAAL_HOME=/usr/local/graalvm-ce-java11-20.2.0 && \
    ln -s $GRAAL_HOME/bin/native-image /usr/local/bin

USER jboss