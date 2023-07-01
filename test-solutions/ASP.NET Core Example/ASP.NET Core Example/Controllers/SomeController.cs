using Microsoft.AspNetCore.Mvc;

namespace Example.Controllers;

[ApiController]
[Route("[controller]")]
public class SomeController : ControllerBase
{
    private readonly ILogger<SomeController> _logger;

    public SomeController(ILogger<SomeController> logger)
    {
        _logger = logger;
    }

    [HttpGet(Name = "GetWeatherForecast")]
    public IEnumerable<int> Get()
    {
        return new[] { 1, 2 };
    }
}