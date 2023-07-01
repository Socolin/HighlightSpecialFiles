namespace Example.Features.Feature1;

public static class ServiceCollectionExtensions
{
    public static void AddFeature1(this IServiceCollection services)
    {
        services.AddSingleton<IFeatureService1, FeatureService1>();
        services.AddSingleton<ISomethingUtil, SomethingUtil>();
        services.AddSingleton<IThingFactory, ThingFactory>();
    }
}