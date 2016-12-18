# ACME4J Manager - Heroku Deploy

This module is an application that:

 1. Demonstrates how the acme4j-manager module can be configured and used
 2. Is at the same time an application that can be used to manage and deploy SSL certificates on Heroku.

## How it works

This application tries to be as little invasive as possible: It does not need to be boundled with the applications that you actually want to run on Heroku. It can be run as standalone app on your computer locally or can be used with [Heroku Scheduler](https://devcenter.heroku.com/articles/scheduler) which can automatically run the application and thus refresh certificates in an interval.
 
When the app is launched:
1. It connects to the PostgreSQL database that's credentials are supplied in a settings file
2. It checks whether the required tables exists and creates them if not
3. It checks the certificate table for certificates that need renewal (expire in less than 30 days)
4. For each certificate in step 3 it validates the domain by deploying the acme-Token to the actual server using a PUT request
5. It refreshes or regenerates the certificates and saves them to the database.
6. It derives the heroku app name from the domain using Regex/Replace and deploys the certificate to Heroku

## Adding a domain

To add a new domain, simply create a new row in the SQL table `certificates` with expire in the past and all certificates an empty string.. 

## Customization

If you are generally happy with the above workflow, all you need to do is copy the `settings.sample.properties` to `settings.properties`, configure the app to your needs and be done.

If you need further customization, e.g. if you are not happy with the challenge deployment using an HTTP PUT-request, you can simply implement your own `ChallengeFaktory`. When you do this, you can also use DNS authentication rather than HTTP authentication.

## Heroku Scheduler

TODO: Test deployment with Heroku Scheduler and write some instructions

## Alternatives

- [substrakt](https://github.com/substrakt/letsencrypt-heroku): Written in Ruby, works only with DNS authentication with CloudFlare. Unfortunately I don't have any Ruby experience to adapt.
- [dehydrated](https://github.com/lukas2511/dehydrated): Written in bash, has many hooks which makes customization possible. I used dehydrated before creating ACME4J Manager and can confirm that the same is possible with ACME4J Manager can be generally achieved with dehydrated as well, but I am much more comfortable with working with Java for developing the Hooks etc. 
