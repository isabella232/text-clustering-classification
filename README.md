About
======

This is a collection of Scala projects to study text clustering and classification. The projects are:

- batch-cluster: A Scala executable that submits a job to a Spark installation.
- infop-expo: A web application in Play framework that resolves multiple data sets with the batch cluster.

Setup
=====

Prerequisites
--------------

1. VirtualBox
1. Vagrant
1. [Chef Development Kit (DK)](https://downloads.chef.io/chef-dk/)

Steps
-----

### VirtualBox

```
vagrant up dev
```

This will install all the dependencies and data files. This might take a couple of hours, so get some coffee and something to read on the side.

Once the VM is up and running, restart the VM.

```
vagrant reload dev
```

### AWS

You will need the *all_purpose* private key for this. Or, you can use your own key and replace these lines in *Vagrantfile*:

```
aws.keypair_name = "your_key_name"
override.ssh.private_key_path = "path/to/your/key.pem"
```

Next, create the AWS instance with:

```
vagrant up awsdemo --provider aws
```

Once it is running, restart it:

```
vagrant reload awsdemo
```
